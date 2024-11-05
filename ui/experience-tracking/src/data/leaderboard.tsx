export interface ExperienceMember {
    experience: bigint;
    rank: number;
    id: string;
    level: number;
    messages: bigint;
    member: Member | null;
    role: Role | null;
    experienceToNextLevel: number;
    experienceOnCurrentLevel: number;
    percentage: number;
    nextLevelExperienceNeeded: number;
    currentLevelExperienceNeeded: number;
}

export interface Member {
    avatarUrl: string;
    name: string;
    id: string;
}

export interface Role {
    r: number | null;
    g: number | null;
    b: number | null;
    name: string | null;
    id: string;
}

export interface ExperienceRole {
    role: Role;
    level: number;
}

export interface ExperienceConfig {
    roles: Array<ExperienceRole>;
}

export interface GuildInfo {
    id: string;
    name: string;
    iconUrl: string | null;
    bannerUrl: string | null;
}