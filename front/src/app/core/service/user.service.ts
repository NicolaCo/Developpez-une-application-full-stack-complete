import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UpdateProfileRequest, UpdatedUserProfile, UserProfile } from '../model/user.interface';

@Injectable({
    providedIn: 'root'
})
export class UserService {

    private pathService = environment.apiUrl + 'users/me';

    private httpClient = inject(HttpClient);

    public profile(): Observable<UserProfile> {
        return this.httpClient.get<UserProfile>(this.pathService);
    }

    public update(request: UpdateProfileRequest): Observable<UpdatedUserProfile> {
        return this.httpClient.put<UpdatedUserProfile>(this.pathService, request);
    }
}
