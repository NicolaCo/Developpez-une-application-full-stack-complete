import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { MaterialModule } from '../../../shared/material.module';
import { TopicService } from '../../core/service/topic.service';
import { UserService } from '../../core/service/user.service';
import { AuthService } from '../../core/service/auth.service';
import { Topic } from '../../core/model/topic.interface';
import { UserProfile } from '../../core/model/user.interface';

@Component({
  imports: [MaterialModule],
  selector: 'app-profile',
  styleUrl: './profile.component.scss',
  templateUrl: './profile.component.html',
})
export class ProfileComponent {
  private formBuilder = inject(FormBuilder);
  private topicService = inject(TopicService);
  private userService = inject(UserService);
  private authService = inject(AuthService);

  protected readonly topicsList = toSignal(this.topicService.all(), { initialValue: [] as Topic[] });
  protected readonly subscribedTopics = computed(() => this.topicsList().filter(topic => topic.isSubscribed));
  protected readonly user = signal<UserProfile | null>(null);
  protected onError = false;

  protected form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    username: ['', [Validators.required]],
    password: ['']
  });

  constructor() {
    this.userService.profile().subscribe({
      next: user => {
        this.user.set(user);
        this.form.patchValue({ email: user.email, username: user.username });
      },
      error: () => this.onError = true
    });
  }

  protected onSubmit(): void {
    this.onError = false;
    if (this.form.invalid) {
      this.onError = true;
      return;
    }

    const { email, username, password } = this.form.getRawValue();
    this.userService.update({
      email,
      username,
      ...(password ? { password } : {})
    }).subscribe({
      next: updated => {
        this.user.set({ id: updated.id, email: updated.email, username: updated.username });
        this.authService.updateToken(updated.token);
        this.form.patchValue({ password: '' });
      },
      error: () => this.onError = true
    });
  }
}
