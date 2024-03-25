export interface ExperienceMember {
    experience: bigint;
    id: bigint;
    level: number;
    messages: bigint;
    member: Member | null;
    role: Role | null;
}

export interface Member {
    avatarUrl: string;
    name: string;
    id: bigint;
}

export interface Role {
    r: number | null;
    g: number | null;
    b: number | null;
    name: string | null;
    id: number;
}

export interface ExperienceRole {
    role: Role;
    level: number;
}

export interface ExperienceConfig {
    roles: Array<ExperienceRole>;
}

export interface GuildInfo {
    id: number;
    name: string;
    iconUrl: string | null;
    bannerUrl: string | null;
}