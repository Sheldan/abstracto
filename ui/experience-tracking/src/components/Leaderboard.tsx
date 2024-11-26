import {LeaderboardEntry} from "./LeaderboardEntry";
import {useEffect, useState} from "react";
import {ExperienceMember, GuildInfo} from "../data/leaderboard";
import {ExperienceConfigDisplay} from "./ExperienceConfigDisplay";
import {ErrorDisplay} from "./ErrorDisplay";

export function Leaderboard({serverId, userId}: { serverId: bigint, userId: bigint }) {

    const pageSize = 50;
    const windowSize = 25;

    const [members, setMembers] = useState<ExperienceMember[]>([])
    const [memberCount, setMemberCount] = useState(0)
    const [pageCountEnd, setPageCountEnd] = useState(0)
    const [pageCountStart, setPageCountStart] = useState(0)
    const [pageOffsetEnd, setPageOffsetEnd] = useState(0)
    const [pageOffsetStart, setPageOffsetStart] = useState(0)
    const [hasMoreAfterwards, setHasMoreAfterwards] = useState(true)
    const [hasMoreBefore, setHasMoreBefore] = useState(true)
    const [userSpecific, setUserSpecific] = useState(false)
    const [hasError, setError] = useState(false)
    const [guildInfo, setGuildInfo] = useState<GuildInfo>({} as GuildInfo)

    async function loadLeaderboardForGuild(page: number, size: number, takeStart: number, skipStart: number, addStart: boolean) {
        try {
            const leaderboardResponse = await fetch(`/experience/v1/leaderboards/${serverId}?page=${page}&size=${size}`)
            const leaderboardJson = await leaderboardResponse.json()
            let loadedMembers: ExperienceMember[] = leaderboardJson.content;
            if(takeStart !== 0) {
                loadedMembers = loadedMembers.slice(0, takeStart)
            }
            if(skipStart !== 0) {
                loadedMembers = loadedMembers.slice(skipStart, loadedMembers.length)
            }
            setMemberCount(memberCount + loadedMembers.length)
            if(hasMoreBefore) {
                setHasMoreBefore(!leaderboardJson.first)
            }
            if(hasMoreAfterwards) {
                setHasMoreAfterwards(!leaderboardJson.last)
            }
            if(addStart) {
                members.unshift(...loadedMembers)
                setMembers(members)
            } else {
                setMembers(members.concat(loadedMembers))
            }
        } catch (error) {
            console.log(error)
            setError(true)
        }
    }


    async function loadLeaderboardForUser(userId: bigint, windowSize: number) {
        try {
            const leaderboardResponse = await fetch(`/experience/v1/leaderboards/${serverId}/${userId}?windowSize=${windowSize}`)
            const loadedMembers: Array<ExperienceMember> = await leaderboardResponse.json();
            setMemberCount(memberCount + loadedMembers.length)
            if(windowSize === loadedMembers.length) { // simple case, we got back the full package
                setHasMoreBefore(true)
                setHasMoreAfterwards(true)
            } else {
                const indexOfUser = loadedMembers.findIndex(value => value.id === userId.toString())
                if(indexOfUser < (windowSize / 2)) { // the user is in the upper half
                    setHasMoreBefore(false)
                } else {
                    setHasMoreBefore(true)
                }
                if((windowSize - indexOfUser) < (windowSize / 2)) { // not the full window was reached
                    setHasMoreAfterwards(false)
                } else {
                    setHasMoreAfterwards(true)
                }
            }
            setMembers(members.concat(loadedMembers))
            const lastRank = loadedMembers[loadedMembers.length -1].rank;
            let lastPage = Math.floor(lastRank / pageSize)
            const pageOffsetEnd = lastRank % pageSize
            setPageOffsetEnd(pageOffsetEnd) // this is how far we got in the last page, take everything starting here
            setPageCountEnd(lastPage) // this is the page the last entry is on, the next page we need to load

            const firstRank = loadedMembers[0].rank;
            const firstPage = Math.floor(firstRank / pageSize)
            const pageOffsetStart = firstRank % pageSize - firstPage * pageSize - 1
            setPageOffsetStart(pageOffsetStart) // this is how many we want to use, starting from the top
            setPageCountStart(firstPage) // this the page we want to load
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
            if(userId === 0n) {
                loadLeaderboardForGuild(0, pageSize, pageSize, 0, false)
            } else {
                setUserSpecific(true)
                loadLeaderboardForUser(userId, windowSize)
            }
        }
        loadGuildInfo()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    },[])

    async function loadMore() {
        await loadLeaderboardForGuild(pageCountEnd + 1, pageSize, pageSize, 0, false)
        setPageCountEnd(pageCountEnd + 1)
    }

    async function loadBefore() {
        await loadLeaderboardForGuild(pageCountStart, pageSize, pageOffsetStart !== 0 ? pageOffsetStart : 0, 0, true)
        setPageOffsetStart(0)
        setPageCountStart(pageCountStart - 1)
    }

    async function loadAfter() {
        await loadLeaderboardForGuild(pageCountEnd, pageSize, pageOffsetEnd !== 0 ? 0 : pageSize, pageOffsetEnd, false)
        setPageOffsetEnd(0)
        setPageCountEnd(pageCountEnd + 1)
    }
    let loadMoreButton = <button className="w-full h-10 bg-gray-500 hover:bg-gray-700 text-white mt-4" onClick={loadMore}>Load more</button>;
    let loadBeforeButton = <button className="w-full h-10 bg-gray-500 hover:bg-gray-700 text-white mt-4" onClick={loadBefore}>Load before</button>;
    let loadAfterButton = <button className="w-full h-10 bg-gray-500 hover:bg-gray-700 text-white mt-4" onClick={loadAfter}>Load after</button>;
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
                            {hasMoreBefore && userSpecific ? loadBeforeButton : ''}
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
                            <div>
                                {hasMoreAfterwards && !userSpecific ? loadMoreButton : ''}
                                {hasMoreAfterwards && userSpecific ? loadAfterButton : ''}
                            </div>
                        </div>
                    </div>
                </>
                : <ErrorDisplay/>}
        </>
    );
}

