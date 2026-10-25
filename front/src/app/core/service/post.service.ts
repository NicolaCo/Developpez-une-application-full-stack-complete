import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreatePostRequest, Post, PostOrder } from '../model/post.interface';

@Injectable({
    providedIn: 'root'
})
export class PostService {

    private pathService = environment.apiUrl + 'posts'

    private httpClient = inject(HttpClient);

    public all(order: PostOrder = 'desc'): Observable<Post[]> {
        const params = new HttpParams().set('order', order);
        return this.httpClient.get<Post[]>(this.pathService, { params });
    }

    public detail(id: number): Observable<Post> {
        return this.httpClient.get<Post>(`${this.pathService}/${id}`);
    }

    public create(request: CreatePostRequest): Observable<Post> {
        return this.httpClient.post<Post>(this.pathService, request);
    }
}
