import { DatePipe } from '@angular/common';
import {
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { EmployeeService } from '../../core/employee.service';
import { ToastService } from '../../core/toast.service';
import {
  ApiError,
  Department,
  Employee,
  EmployeeRequest,
  EmploymentStatus,
  Project,
} from '../../core/models';

function noFutureDate(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null;
  }
  const today = new Date().toLocaleDateString('en-CA');
  return control.value > today ? { futureDate: true } : null;
}

@Component({
  selector: 'app-employee-form',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.css',
})
export class EmployeeFormComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly employeeId = signal<number | null>(null);
  readonly isEdit = computed(() => this.employeeId() !== null);
  readonly departments = signal<Department[]>([]);
  readonly projects = signal<Project[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly loadError = signal('');
  readonly formError = signal('');

  readonly maxDate = new Date().toLocaleDateString('en-CA');
  readonly statuses: ReadonlyArray<{ value: EmploymentStatus; label: string }> = [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'ON_LEAVE', label: 'On leave' },
    { value: 'INACTIVE', label: 'Inactive' },
  ];

  readonly form = this.fb.group({
    employeeCode: [
      '',
      [
        Validators.required,
        Validators.maxLength(20),
        Validators.pattern(/^[A-Za-z0-9-]+$/),
      ],
    ],
    firstName: ['', [Validators.required, Validators.maxLength(60)]],
    lastName: ['', [Validators.required, Validators.maxLength(60)]],
    email: [
      '',
      [Validators.required, Validators.email, Validators.maxLength(150)],
    ],
    phone: ['', [Validators.pattern(/^$|^[0-9+()\-\s]{7,20}$/)]],
    designation: ['', [Validators.required, Validators.maxLength(100)]],
    departmentId: [null as number | null, [Validators.required]],
    projectId: [null as number | null],
    dateOfJoining: ['', [Validators.required, noFutureDate]],
    salary: [
      null as number | null,
      [Validators.required, Validators.min(1)],
    ],
    status: ['ACTIVE' as EmploymentStatus, [Validators.required]],
  });

  constructor() {
    this.form.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.clearServerErrors();
        this.formError.set('');
      });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam !== null ? Number(idParam) : null;
    this.loadOptions(id);
  }

  fieldError(name: string): string {
    const control = this.form.get(name);
    if (!control || !control.touched) {
      return '';
    }
    if (control.hasError('server')) {
      return control.getError('server') as string;
    }
    if (control.hasError('required')) {
      return 'This field is required.';
    }
    if (control.hasError('email')) {
      return 'Enter a valid email address.';
    }
    if (control.hasError('maxlength')) {
      return 'This value is too long.';
    }
    if (control.hasError('min')) {
      return 'Enter a value greater than zero.';
    }
    if (control.hasError('futureDate')) {
      return 'Date of joining cannot be in the future.';
    }
    if (control.hasError('pattern')) {
      return name === 'phone'
        ? 'Use digits, spaces, +, - or brackets (7–20 characters).'
        : 'Use letters, numbers and hyphens only.';
    }
    return '';
  }

  previewName(): string {
    const first = (this.form.controls.firstName.value ?? '').trim();
    const last = (this.form.controls.lastName.value ?? '').trim();
    return [first, last].filter(Boolean).join(' ') || 'New employee';
  }

  previewInitials(): string {
    const first = (this.form.controls.firstName.value ?? '').trim().charAt(0);
    const last = (this.form.controls.lastName.value ?? '').trim().charAt(0);
    const initials = `${first}${last}`.toUpperCase();
    return initials || 'EP';
  }

  previewDepartment(): string {
    const id = this.form.controls.departmentId.value;
    return this.departments().find((department) => department.id === id)?.name ?? 'Not selected';
  }

  previewProject(): string {
    const id = this.form.controls.projectId.value;
    if (id === null) {
      return 'No project';
    }
    return this.projects().find((project) => project.id === id)?.name ?? 'No project';
  }

  previewStatusLabel(): string {
    const status = this.form.controls.status.value;
    return this.statuses.find((option) => option.value === status)?.label ?? 'Active';
  }

  previewStatusClass(): string {
    return `badge badge-${(this.form.controls.status.value ?? 'ACTIVE').toLowerCase()}`;
  }

  submit(): void {
    this.formError.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.formError.set('Please correct the highlighted fields before saving.');
      return;
    }

    const raw = this.form.getRawValue();
    const request: EmployeeRequest = {
      employeeCode: (raw.employeeCode ?? '').trim().toUpperCase(),
      firstName: (raw.firstName ?? '').trim(),
      lastName: (raw.lastName ?? '').trim(),
      email: (raw.email ?? '').trim().toLowerCase(),
      phone: (raw.phone ?? '').trim() || null,
      designation: (raw.designation ?? '').trim(),
      salary: Number(raw.salary ?? 0),
      dateOfJoining: raw.dateOfJoining ?? '',
      status: raw.status ?? 'ACTIVE',
      departmentId: Number(raw.departmentId),
      projectId: raw.projectId !== null ? Number(raw.projectId) : null,
    };

    const id = this.employeeId();
    this.saving.set(true);

    const request$ =
      id !== null
        ? this.employeeService.update(id, request)
        : this.employeeService.create(request);

    request$.subscribe({
      next: (employee) => {
        this.saving.set(false);
        this.toast.success(
          id !== null
            ? `${employee.fullName} was updated successfully.`
            : `${employee.fullName} was added to the directory.`,
        );
        void this.router.navigate(['/employees']);
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.applyApiError(error);
      },
    });
  }

  private loadOptions(id: number | null): void {
    this.loading.set(true);
    this.loadError.set('');

    forkJoin({
      departments: this.employeeService.getDepartments(),
      projects: this.employeeService.getProjects(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ departments, projects }) => {
          this.departments.set(departments);
          this.projects.set(projects);

          if (id !== null) {
            this.loadEmployee(id);
          } else {
            this.loading.set(false);
          }
        },
        error: () => {
          this.loading.set(false);
          this.loadError.set(
            'The form could not be prepared. Check that the Spring Boot API is running on port 8080.',
          );
        },
      });
  }

  private loadEmployee(id: number): void {
    this.employeeService.getById(id).subscribe({
      next: (employee: Employee) => {
        this.employeeId.set(id);
        this.form.patchValue({
          employeeCode: employee.employeeCode,
          firstName: employee.firstName,
          lastName: employee.lastName,
          email: employee.email,
          phone: employee.phone ?? '',
          designation: employee.designation,
          departmentId: employee.department.id,
          projectId: employee.project?.id ?? null,
          dateOfJoining: employee.dateOfJoining,
          salary: employee.salary,
          status: employee.status,
        });
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.loadError.set(
          'This employee could not be loaded. The record may have been removed.',
        );
      },
    });
  }

  private clearServerErrors(): void {
    Object.values(this.form.controls).forEach((control) => {
      if (control.hasError('server')) {
        const errors = { ...(control.errors ?? {}) };
        delete errors['server'];
        control.setErrors(Object.keys(errors).length > 0 ? errors : null);
      }
    });
  }

  private applyApiError(error: unknown): void {
    const body = (error as { error?: ApiError }).error;

    if (body?.fieldErrors) {
      Object.entries(body.fieldErrors).forEach(([field, message]) => {
        const control = this.form.get(field);
        if (control) {
          control.setErrors({ ...(control.errors ?? {}), server: message });
          control.markAsTouched();
        }
      });
    }

    this.formError.set(
      body?.message ?? 'The employee could not be saved. Please check the values and try again.',
    );
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
