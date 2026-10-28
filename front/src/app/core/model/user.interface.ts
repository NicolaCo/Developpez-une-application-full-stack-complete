export interface UserProfile {
    id: number;
    email: string;
    username: string;
}

export interface UpdateProfileRequest {
    email: string;
    username: string;
    password?: string;
}

export interface UpdatedUserProfile extends UserProfile {
    token: string;
}
