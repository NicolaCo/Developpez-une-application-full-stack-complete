import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { AuthService } from '../../core/service/auth.service';

@Component({
  imports: [MaterialModule],
  selector: 'app-login',
  styleUrl: './login.component.scss',
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  public hide = true;
  public onError = false;

  public form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  public onSubmit(): void {
    this.onError = false;
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        const returnUrl = this.returnUrl();
        if (returnUrl && returnUrl !== '/login' && returnUrl !== '/signup') {
          this.router.navigateByUrl(returnUrl);
        } else {
          this.router.navigate(['/']);
        }
      },
      error: () => this.onError = true
    });
  }

  private returnUrl(): string {
    return this.router.parseUrl(this.router.url).queryParams['returnUrl'] ?? '/topics';
  }
}
