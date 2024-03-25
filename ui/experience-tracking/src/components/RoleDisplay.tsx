import {Role} from "../data/leaderboard";
import createStyle from "../utils/styleUtils";

export const RoleDisplay = ({role}: { role: Role | null }) => {
    const roleColor = createStyle(role);
    let roleDisplay = role !== null && role.name !== null ? <span style={{ color: roleColor}}>{role.name}</span> : <>Deleted role {role !== null ? role!.id : ''}</>
    return (
        <>
            {roleDisplay}
        </>
    );
}