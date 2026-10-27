import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, EMPTY } from 'rxjs';
import { MaterialModule } from '../../../shared/material.module';
import { AuthService } from '../../core/service/auth.service';

function passwordStrengthValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    if (!value) {
      return null;
    }
    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumeric = /[0-9]/.test(value);
    const hasSpecialChar = /[^A-Za-z0-9]/.test(value);
    const valid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecialChar && value.length >= 8;
    return valid ? null : { passwordStrength: true };
  };
}

@Component({
  imports: [MaterialModule, RouterLink],
  selector: 'app-signup',
  styleUrl: './signup.component.scss',
  templateUrl: './signup.component.html',
})
export class SignupComponent {
  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  public hide = true;
  public onError = false;

  public form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    username: ['', [Validators.required]],
    password: ['', [Validators.required, passwordStrengthValidator()]]
  });

  public onSubmit(): void {
    this.onError = false;
    if (this.form.invalid) {
      this.onError = true;
      this.cdr.detectChanges();
      return;
    }
    this.authService.register(this.form.getRawValue()).pipe(
      catchError(() => {
        this.onError = true;
        this.cdr.detectChanges();
        return EMPTY;
      })
    ).subscribe({
      next: () => this.router.navigate(['/'])
    });
  }
}
