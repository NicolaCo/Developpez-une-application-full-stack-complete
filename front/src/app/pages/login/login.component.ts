import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, EMPTY } from 'rxjs';
import { MaterialModule } from '../../../shared/material.module';
import { AuthService } from '../../core/service/auth.service';

@Component({
  imports: [MaterialModule, RouterLink],
  selector: 'app-login',
  styleUrl: './login.component.scss',
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  public hide = true;
  public onError = false;

  public form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  public onSubmit(): void {
    this.onError = false;
    if (this.form.invalid) {
      this.onError = true;
      this.cdr.detectChanges();
      return;
    }
    this.authService.login(this.form.getRawValue()).pipe(
      catchError(() => {
        this.onError = true;
        this.cdr.detectChanges();
        return EMPTY;
      })
    ).subscribe({
      next: () => {
        const returnUrl = this.returnUrl();
        if (this.isSafeInternalUrl(returnUrl)) {
          this.router.navigateByUrl(returnUrl);
        } else {
          this.router.navigate(['/']);
        }
      }
    });
  }

  private returnUrl(): string | null {
    return this.router.parseUrl(this.router.url).queryParams['returnUrl'] ?? null;
  }

  private isSafeInternalUrl(url: string | null): url is string {
    return !!url
      && url.startsWith('/')
      && !url.startsWith('//')
      && url !== '/'
      && url !== '/login'
      && url !== '/signup';
  }
}
