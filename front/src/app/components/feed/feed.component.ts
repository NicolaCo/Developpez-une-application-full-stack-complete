import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MaterialModule } from '../../shared/material.module';
import { PostService } from '../../core/service/post.service';
import { Post, PostOrder } from '../../core/model/post.interface';

@Component({
  imports: [MaterialModule, RouterLink, DatePipe],
  selector: 'app-feed',
  styleUrl: './feed.component.scss',
  templateUrl: './feed.component.html',
})
export class FeedComponent {

  private postService = inject(PostService);
  private router = inject(Router);

  protected readonly order = signal<PostOrder>('desc');
  protected readonly posts = signal<Post[]>([]);

  constructor() {
    this.load();
  }

  protected toggleOrder(): void {
    this.order.update(current => (current === 'desc' ? 'asc' : 'desc'));
    this.load();
  }

  protected openDetail(post: Post): void {
    this.router.navigate(['/posts', post.id]);
  }

  private load(): void {
    this.postService.all(this.order()).subscribe(posts => this.posts.set(posts));
  }
}
