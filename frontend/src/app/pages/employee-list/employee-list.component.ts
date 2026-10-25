import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmployeeService } from '../../core/employee.service';
import { ToastService } from '../../core/toast.service';
import {
  Department,
  Employee,
  EmploymentStatus,
} from '../../core/models';

type StatusFilter = EmploymentStatus | 'ALL';
type DepartmentFilter = number | 'ALL';

@Component({
  selector: 'app-employee-list',
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.css',
})
export class EmployeeListComponent {
  private readonly employeeService = inject(EmployeeService);
  private readonly toast = inject(ToastService);

  readonly employees = signal<Employee[]>([]);
  readonly departments = signal<Department[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly searchTerm = signal('');
  readonly statusFilter = signal<StatusFilter>('ALL');
  readonly departmentFilter = signal<DepartmentFilter>('ALL');
  readonly deleteTarget = signal<Employee | null>(null);
  readonly deleting = signal(false);

  readonly statusOptions: ReadonlyArray<{ value: StatusFilter; label: string }> = [
    { value: 'ALL', label: 'All statuses' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'ON_LEAVE', label: 'On leave' },
    { value: 'INACTIVE', label: 'Inactive' },
  ];

  readonly filteredEmployees = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const status = this.statusFilter();
    const department = this.departmentFilter();

    return this.employees().filter((employee) => {
      const haystack = [
        employee.fullName,
        employee.employeeCode,
        employee.email,
        employee.designation,
        employee.department.name,
      ]
        .join(' ')
        .toLowerCase();

      const matchesTerm = !term || haystack.includes(term);
      const matchesStatus = status === 'ALL' || employee.status === status;
      const matchesDepartment =
        department === 'ALL' || employee.department.id === department;

      return matchesTerm && matchesStatus && matchesDepartment;
    });
  });

  readonly activeCount = computed(
    () => this.employees().filter((employee) => employee.status === 'ACTIVE').length,
  );

  readonly onLeaveCount = computed(
    () => this.employees().filter((employee) => employee.status === 'ON_LEAVE').length,
  );

  readonly departmentCount = computed(
    () => new Set(this.employees().map((employee) => employee.department.id)).size,
  );

  readonly hasFilters = computed(
    () =>
      this.searchTerm().trim().length > 0 ||
      this.statusFilter() !== 'ALL' ||
      this.departmentFilter() !== 'ALL',
  );

  constructor() {
    this.loadEmployees();
    this.loadDepartments();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.deleteTarget() && !this.deleting()) {
      this.closeDelete();
    }
  }

  loadEmployees(): void {
    this.loading.set(true);
    this.error.set('');

    this.employeeService.list().subscribe({
      next: (employees) => {
        this.employees.set(employees);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set(
          'The employee directory could not be loaded. Check that the Spring Boot API is running on port 8080.',
        );
      },
    });
  }

  loadDepartments(): void {
    this.employeeService.getDepartments().subscribe({
      next: (departments) => this.departments.set(departments),
      error: () => {
        this.departments.set([]);
      },
    });
  }

  onSearch(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
  }

  onStatusChange(event: Event): void {
    this.statusFilter.set((event.target as HTMLSelectElement).value as StatusFilter);
  }

  onDepartmentChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.departmentFilter.set(value === 'ALL' ? 'ALL' : Number(value));
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('ALL');
    this.departmentFilter.set('ALL');
  }

  requestDelete(employee: Employee): void {
    this.deleteTarget.set(employee);
  }

  closeDelete(): void {
    if (!this.deleting()) {
      this.deleteTarget.set(null);
    }
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) {
      return;
    }

    this.deleting.set(true);
    this.employeeService.delete(target.id).subscribe({
      next: () => {
        this.employees.update((employees) =>
          employees.filter((employee) => employee.id !== target.id),
        );
        this.deleting.set(false);
        this.deleteTarget.set(null);
        this.toast.success(`${target.fullName} was removed from the directory.`);
      },
      error: () => {
        this.deleting.set(false);
        this.toast.error('The employee could not be deleted. Please try again.');
      },
    });
  }

  statusLabel(status: EmploymentStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'Active';
      case 'ON_LEAVE':
        return 'On leave';
      case 'INACTIVE':
        return 'Inactive';
    }
  }

  statusClass(status: EmploymentStatus): string {
    return `badge badge-${status.toLowerCase()}`;
  }

  initials(employee: Employee): string {
    return `${employee.firstName.charAt(0)}${employee.lastName.charAt(0)}`.toUpperCase();
  }

  avatarClass(employee: Employee): string {
    return `avatar avatar-${(employee.id % 5) + 1}`;
  }
}
