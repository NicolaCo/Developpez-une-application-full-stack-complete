import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Comment, CreateCommentRequest } from '../model/comment.interface';

@Injectable({
    providedIn: 'root'
})
export class CommentService {

    private httpClient = inject(HttpClient);

    public all(postId: number): Observable<Comment[]> {
        return this.httpClient.get<Comment[]>(this.path(postId));
    }

    public create(postId: number, request: CreateCommentRequest): Observable<Comment> {
        return this.httpClient.post<Comment>(this.path(postId), request);
    }

    private path(postId: number): string {
        return `${environment.apiUrl}posts/${postId}/comments`;
    }
}
