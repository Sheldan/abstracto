import {ExperienceMember} from "../data/leaderboard";
import {RoleDisplay} from "./RoleDisplay";
import createStyle from "../utils/styleUtils";

export const LeaderboardEntry = ({member}: { member: ExperienceMember }) => {
    const userHasRole = member.role !== null;
    const memberExists = member.member !== null;
    const nameColor = userHasRole ? createStyle(member.role!) : ''
    let memberDisplay = memberExists ? <>
        <img alt={member.member!.name} src={member.member!.avatarUrl}
                    className="object-contain h-16 w-16 rounded-full"/>
        <span className="align-middle" style={{color: nameColor}}>{member.member!.name}</span>
    </> : <>{member.id}</>;
    return (
        <>
            <tr className="border-b bg-gray-800 border-gray-700">
                <td
                    className="px-2 py-4 font-medium whitespace-nowrap text-white flex items-center gap-3">
                    {memberDisplay}
                </td>
                <td className="px-6 py-4 text-center">
                    {member.experience.toLocaleString()}
                </td>
                <td className="px-6 py-4 text-center">
                    {member.messages.toLocaleString()}
                </td>
                <td className="px-6 py-4 text-center">
                    {member.level.toString()}
                </td>
                <td className="px-6 py-4 text-center">
                    {userHasRole ? <RoleDisplay role={member.role!}/> : 'No role'}
                </td>
            </tr>

        </>
    );
}