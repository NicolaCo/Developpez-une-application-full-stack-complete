import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Topic } from '../model/topic.interface';


@Injectable({
    providedIn: 'root'
})
export class TopicService {

    private pathService = environment.apiUrl + 'topics'

    private httpClient = inject(HttpClient);

    public all(): Observable<Topic[]> {
        return this.httpClient.get<Topic[]>(this.pathService);
    } 

}
