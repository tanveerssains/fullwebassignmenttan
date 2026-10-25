import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info';

export interface ToastMessage {
  id: number;
  text: string;
  type: ToastType;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private sequence = 0;
  private timer?: ReturnType<typeof setTimeout>;
  readonly message = signal<ToastMessage | null>(null);

  success(text: string): void {
    this.show(text, 'success');
  }

  error(text: string): void {
    this.show(text, 'error');
  }

  info(text: string): void {
    this.show(text, 'info');
  }

  dismiss(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = undefined;
    }
    this.message.set(null);
  }

  private show(text: string, type: ToastType): void {
    if (this.timer) {
      clearTimeout(this.timer);
    }
    this.sequence += 1;
    this.message.set({ id: this.sequence, text, type });
    this.timer = setTimeout(() => this.message.set(null), 4000);
  }
}
