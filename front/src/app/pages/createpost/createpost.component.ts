import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { MaterialModule } from '../../../shared/material.module';
import { PostService } from '../../core/service/post.service';
import { TopicService } from '../../core/service/topic.service';
import { Topic } from '../../core/model/topic.interface';

@Component({
  imports: [MaterialModule],
  selector: 'app-createpost',
  styleUrl: './createpost.component.scss',
  templateUrl: './createpost.component.html',
})
export class CreatepostComponent {

  private formBuilder = inject(FormBuilder);
  private postService = inject(PostService);
  private topicService = inject(TopicService);
  private router = inject(Router);

  protected readonly topics = toSignal(this.topicService.all(), { initialValue: [] as Topic[] });

  protected onError = false;

  protected form = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required]],
    content: ['', [Validators.required]],
    topicId: [0, [Validators.required, Validators.min(1)]]
  });

  protected onSubmit(): void {
    this.onError = false;
    this.postService.create(this.form.getRawValue()).subscribe({
      next: post => this.router.navigate(['/posts', post.id]),
      error: () => this.onError = true
    });
  }
}
