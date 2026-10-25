import { Component, inject } from '@angular/core';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-toast',
  template: `
    @if (toast.message(); as message) {
      <div class="toast" [class]="'toast-' + message.type" role="status" aria-live="polite">
        <span class="toast-icon" aria-hidden="true">
          @switch (message.type) {
            @case ('success') {
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="m5 13 4 4L19 7" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            }
            @case ('error') {
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 8v5m0 3h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            }
            @default {
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="9" />
                <path d="M12 11v5m0-8h.01" stroke-linecap="round" />
              </svg>
            }
          }
        </span>
        <span class="toast-text">{{ message.text }}</span>
        <button class="toast-close" type="button" (click)="toast.dismiss()" aria-label="Dismiss notification">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="m6 6 12 12M18 6 6 18" stroke-linecap="round" />
          </svg>
        </button>
      </div>
    }
  `,
  styles: `
    :host { position: fixed; right: 24px; bottom: 24px; z-index: 100; }
    .toast {
      display: flex; align-items: center; gap: 12px; min-width: 280px; max-width: 420px;
      padding: 13px 14px; border-radius: 12px; background: #fff; color: var(--text);
      border: 1px solid var(--border); box-shadow: 0 16px 40px rgba(15, 23, 42, 0.18);
      animation: toast-in 0.25s ease-out;
    }
    .toast-icon {
      display: grid; place-items: center; width: 30px; height: 30px; border-radius: 50%;
      flex-shrink: 0;
    }
    .toast-icon svg { width: 17px; height: 17px; }
    .toast-success .toast-icon { background: var(--success-soft); color: var(--success); }
    .toast-error .toast-icon { background: var(--danger-soft); color: var(--danger); }
    .toast-info .toast-icon { background: var(--primary-soft); color: var(--primary); }
    .toast-text { flex: 1; font-size: 0.875rem; font-weight: 500; line-height: 1.4; }
    .toast-close {
      display: grid; place-items: center; width: 26px; height: 26px; border: 0;
      background: transparent; color: var(--muted); border-radius: 6px; cursor: pointer;
    }
    .toast-close:hover { background: var(--surface-alt); color: var(--text); }
    .toast-close svg { width: 15px; height: 15px; }
    @keyframes toast-in {
      from { opacity: 0; transform: translateY(12px) scale(0.98); }
      to { opacity: 1; transform: translateY(0) scale(1); }
    }
    @media (max-width: 560px) {
      :host { left: 16px; right: 16px; bottom: 16px; }
      .toast { min-width: 0; max-width: none; width: 100%; }
    }
  `,
})
export class ToastComponent {
  protected readonly toast = inject(ToastService);
}
