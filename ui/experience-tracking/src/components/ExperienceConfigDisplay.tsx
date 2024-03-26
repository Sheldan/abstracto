import {ExperienceConfig, ExperienceRole} from "../data/leaderboard";
import {RoleDisplay} from "./RoleDisplay";
import {useEffect, useState} from "react";

export const ExperienceConfigDisplay = ({serverId}: { serverId: bigint }) => {

    const [roles, setRoles] = useState<ExperienceRole[]>([])
    const [hasError, setError] = useState(false)
    const [isOpen, setOpen] = useState(false)
    async function loadConfig() {
        try {
            const configResponse = await fetch(`/experience/v1/leaderboards/${serverId}/config`)
            let configObj: ExperienceConfig = await configResponse.json();
            const roles = configObj.roles;
            setRoles(roles)
        } catch (error) {
            console.log(error)
            setError(true)
        }
    }

    useEffect(()=> {
        loadConfig()
    // eslint-disable-next-line react-hooks/exhaustive-deps
    },[])

    function toggleOpen() {
        setOpen(!isOpen)
    }

    return (
        <>
            {!hasError && roles.length !== 0 ?
                <div className="bg-gray-800 p-4 mx-auto w-4/5 rounded-lg">
                    <button className="w-full flex justify-between items-center p-2 px-4" onClick={toggleOpen}>
                        <h2 className="text-xl font-extrabold leading-none tracking-tight text-gray-100">Role config</h2>
                            <div>
                                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className={`w-6 h-6 stroke-white stroke-2 duration-500 ${isOpen ? "rotate-180" : ""}`}>
                                    <path strokeLinecap="round" strike-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5"></path>
                                </svg>
                            </div>
                    </button>
                    <div className={`grid grid-cols-1 ${isOpen ? 'grid-rows-[1fr]' : 'grid-rows-[0fr]'} overflow-hidden duration-500`}>
                        <div className="w-full min-h-0 overflow-hidden">
                            <div className="p-4 flex flex-col gap-3 items-start">
                            {roles.map(role =>
                                <div key={role.role.id} className="border-gray-700 flex gap-4 items-center flex-row-reverse">
                                    <p>
                                        <RoleDisplay role={role.role}/>
                                    </p>
                                    <p className="font-bold text-gray-200">
                                        {role.level}
                                    </p>
                                </div>)}
                            </div>
                        </div>
                    </div>
                </div>
            : ''}
        </>
    );
}