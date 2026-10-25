import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MaterialModule } from '../../../shared/material.module';
import { AuthService } from '../../core/service/auth.service';

@Component({
  imports: [MaterialModule],
  selector: 'app-signup',
  styleUrl: './signup.component.scss',
  templateUrl: './signup.component.html',
})
export class SignupComponent {
  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  public hide = true;
  public onError = false;

  public form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    username: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  public onSubmit(): void {
    this.onError = false;
    this.authService.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.onError = true
    });
  }
}
