import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'employees',
  },
  {
    path: 'employees',
    title: 'Employees · Employee Portal',
    loadComponent: () =>
      import('./pages/employee-list/employee-list.component').then(
        (module) => module.EmployeeListComponent,
      ),
  },
  {
    path: 'employees/new',
    title: 'Add employee · Employee Portal',
    loadComponent: () =>
      import('./pages/employee-form/employee-form.component').then(
        (module) => module.EmployeeFormComponent,
      ),
  },
  {
    path: 'employees/:id/edit',
    title: 'Edit employee · Employee Portal',
    loadComponent: () =>
      import('./pages/employee-form/employee-form.component').then(
        (module) => module.EmployeeFormComponent,
      ),
  },
  {
    path: '**',
    redirectTo: 'employees',
  },
];
