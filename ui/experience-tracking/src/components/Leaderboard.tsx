import {LeaderboardEntry} from "./LeaderboardEntry";
import {useEffect, useState} from "react";
import {ExperienceMember, GuildInfo} from "../data/leaderboard";
import {ExperienceConfigDisplay} from "./ExperienceConfigDisplay";
import {ErrorDisplay} from "./ErrorDisplay";

export function Leaderboard({serverId}: { serverId: bigint }) {

    const pageSize = 25;

    const [members, setMembers] = useState<ExperienceMember[]>([])
    const [memberCount, setMemberCount] = useState(0)
    const [pageCount, setPageCount] = useState(0)
    const [hasMore, setHasMore] = useState(true)
    const [hasError, setError] = useState(false)
    const [guildInfo, setGuildInfo] = useState<GuildInfo>({} as GuildInfo)

    async function loadLeaderboard(page: number, size: number) {
        try {
            const leaderboardResponse = await fetch(`/experience/v1/leaderboards/${serverId}?page=${page}&size=${size}`)
            const leaderboardJson = await leaderboardResponse.json()
            const loadedMembers: Array<ExperienceMember> = leaderboardJson.content;
            setMemberCount(memberCount + loadedMembers.length)
            setHasMore(!leaderboardJson.last)
            setPageCount(page)
            setMembers(members.concat(loadedMembers))
        } catch (error) {
            console.log(error)
            setError(true)
        }
    }

    async function loadGuildInfo() {
        try {
            const guildInfoResponse = await fetch(`/servers/v1/${serverId}/info`)
            const guildInfoJson: GuildInfo= await guildInfoResponse.json()
            setGuildInfo(guildInfoJson)
        } catch (error) {
            console.log(error)
        }
    }

    useEffect(()=> {
        if(memberCount === 0) {
            loadLeaderboard(0, pageSize)
        }
        loadGuildInfo()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    },[])

    function loadMore() {
        loadLeaderboard(pageCount + 1, pageSize)
    }
    let loadMoreButton = <button className="w-full h-10 bg-gray-500 hover:bg-gray-700 text-white mt-4" onClick={loadMore}>Load more</button>;
    return (
        <>
            {!hasError ?
                <>
                    <div className="relative font-[sans-serif] before:absolute before:w-full before:h-full before:inset-0 before:bg-black before:opacity-50 before:z-10 h-48">
                        {guildInfo.bannerUrl !== null ? <img src={guildInfo.bannerUrl + "?size=4096"}
                                                            alt="Banner"
                                                            className="absolute inset-0 w-full h-full object-cover"/> : ''}
                        <div
                            className="min-h-[150px] relative z-50 h-full max-w-6xl mx-auto flex flex-row justify-center items-center text-center text-white p-6">
                            {guildInfo.iconUrl !== null ? <img
                                src={guildInfo.iconUrl + "?size=512"}
                                alt="Icon"
                                className="w-24"/>
                                : ''}
                            <h1 className="text-4xl font-extrabold leading-none tracking-tight md:text-5xl lg:text-6xl text-white px-5">{guildInfo.name + ' Leaderboard'}</h1>
                        </div>

                    </div>
                    <div className="flex flex-col">
                        <div className="mt-4">
                            <ExperienceConfigDisplay serverId={serverId}/>
                        </div>
                        <div className="text-sm text-left w-full mt-4">
                            <table className="w-full text-gray-400">
                                <thead
                                    className="text-xs uppercase bg-gray-700 text-gray-400">
                                <tr>
                                    <th scope="col" className="px-2 py-3 w-5">
                                        Rank
                                    </th>
                                    <th scope="col" className="px-1 py-3 w-1/2 sm:w-1/2">
                                        Member
                                    </th>
                                    <th scope="col" className="px-1 py-3 w-1/5 sm:w-1/5 text-center">
                                        Experience
                                    </th>
                                    <th scope="col" className="px-1 py-3 w-1/5 sm:w-1/6 text-center">
                                        Messages
                                    </th>
                                    <th scope="col" className="px-1 py-3 w-1/5 text-center">
                                        Level
                                    </th>
                                </tr>
                                </thead>
                                <tbody>
                                {members.map((member, index) => <LeaderboardEntry key={member.id} index={index} member={member}/>)}
                                </tbody>
                            </table>
                            {hasMore ? loadMoreButton : ''}
                        </div>
                    </div>
                </>
                : <ErrorDisplay/>}
        </>
    );
}

