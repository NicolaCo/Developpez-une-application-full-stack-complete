import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, switchMap } from 'rxjs';
import { MaterialModule } from '../../../shared/material.module';
import { PostService } from '../../core/service/post.service';
import { CommentService } from '../../core/service/comment.service';
import { TopicService } from '../../core/service/topic.service';
import { Post } from '../../core/model/post.interface';
import { Comment } from '../../core/model/comment.interface';

@Component({
  imports: [MaterialModule, DatePipe],
  selector: 'app-post-detail',
  styleUrl: './post-detail.component.scss',
  templateUrl: './post-detail.component.html',
})
export class PostDetailComponent {

  private formBuilder = inject(FormBuilder);
  private postService = inject(PostService);
  private commentService = inject(CommentService);
  private topicService = inject(TopicService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  private readonly postId = toSignal(
    this.route.paramMap.pipe(map(params => Number(params.get('id')))),
    { initialValue: 0 }
  );

  protected readonly post = toSignal<Post | null>(
    this.route.paramMap.pipe(
      switchMap(params => this.postService.detail(Number(params.get('id'))))
    ),
    { initialValue: null }
  );

  protected readonly topics = toSignal(this.topicService.all(), { initialValue: [] });

  protected getTopicName(topicId: number | undefined): string {
    if (topicId === undefined || topicId === null) {
      return '';
    }
    const topic = this.topics().find(t => t.id === topicId);
    return topic ? topic.title : '';
  }

  protected readonly comments = signal<Comment[]>([]);
  protected onError = false;

  protected form = this.formBuilder.nonNullable.group({
    content: ['', [Validators.required]]
  });

  constructor() {
    this.loadComments();
  }

  protected onSubmit(): void {
    this.onError = false;
    this.commentService.create(this.postId(), this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset();
        this.loadComments();
      },
      error: () => this.onError = true
    });
  }

  protected back(): void {
    this.router.navigate(['/posts']);
  }

  private loadComments(): void {
    this.commentService.all(this.postId()).subscribe(comments => this.comments.set(comments));
  }
}
