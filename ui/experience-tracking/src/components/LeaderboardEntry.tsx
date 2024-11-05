import {ExperienceMember} from "../data/leaderboard";
import createStyle from "../utils/styleUtils";

export const LeaderboardEntry = ({member, index}: { member: ExperienceMember, index: number }) => {
    const userHasRole = member.role !== null;
    const memberExists = member.member !== null;
    const nameColor = userHasRole ? createStyle(member.role!) : ''
    let memberDisplay = memberExists ? <>
        <img alt={member.member!.name} src={member.member!.avatarUrl}
                    className="object-contain h-16 w-16 rounded-full"/>
        <span className="align-middle max-[480px]:max-w-48 truncate" style={{color: nameColor}}>{member.member!.name}</span>
    </> :
        <span className="inline-flex items-center h-16">
            {member.id.toString()}
        </span>;

    function getDisplayPercentage() {
        let increased = member.percentage * 100;
        if(increased % 100 == 0) {
            return Math.trunc(increased / 100)
        } else {
            return Math.trunc(increased) / 100;
        }
    }

    function getPercentageDisplay() {
        return `${member.experienceOnCurrentLevel.toLocaleString()}/${member.currentLevelExperienceNeeded.toLocaleString()} (${getDisplayPercentage()}%)`
    }

    let memberDetail = <tr>
        <td colSpan={5}>
            <div className={`bg-slate-700`}>
                <div className={`${index % 2 === 0 ? "bg-gray-600" : "bg-gray-800"} text-center`} style={{width: getDisplayPercentage() + '%'}}>
                    <span style={{color: nameColor, whiteSpace: "nowrap"}}>{getPercentageDisplay()}</span>
                </div>
            </div>
        </td>
    </tr>;
    return (
        <>
            <tr className={`${index % 2 === 0 ? "bg-gray-600" : "bg-gray-800"} h-full`} style={{minHeight: 64}}>
                <td
                    className="text-center">
                    {member.rank}
                </td>
                <td className="px-2 py-3 font-medium whitespace-nowrap text-white flex items-center gap-2">
                    {memberDisplay}
                </td>
                <td className="px-1 py-3 text-center" style={{color: nameColor}}>
                    {member.experience.toLocaleString()}
                </td>
                <td className="px-1 py-3 text-center" style={{color: nameColor}}>
                    {member.messages.toLocaleString()}
                </td>
                <td className="px-2 py-3 text-center" style={{color: nameColor}}>
                    {member.level.toString()}
                </td>
            </tr>
                {memberDetail}

        </>
    );
}