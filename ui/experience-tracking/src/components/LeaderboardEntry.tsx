import {ExperienceMember} from "../data/leaderboard";
import createStyle from "../utils/styleUtils";

export const LeaderboardEntry = ({member, index}: { member: ExperienceMember, index: number }) => {
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
            <tr className={`${index % 2 === 0 ? "bg-gray-800" : "bg-gray-600"}`}>
                <td
                    className="text-center">
                    {member.rank}
                </td>
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
            </tr>

        </>
    );
}