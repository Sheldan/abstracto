import {ExperienceConfig, ExperienceRole} from "../data/leaderboard";
import {RoleDisplay} from "./RoleDisplay";
import {useEffect, useState} from "react";

export const ExperienceConfigDisplay = ({serverId}: { serverId: bigint }) => {

    const [roles, setRoles] = useState<ExperienceRole[]>([])
    const [hasError, setError] = useState(false)
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

    return (
        <>
            {!hasError ?
                <div className="py-10">
                    <h2 className="text-4xl font-extrabold leading-none tracking-tight text-white">Role
                        config</h2>
                        <table className="w-full text-gray-400">
                            <thead
                                className="text-xs uppercase bg-gray-700 text-gray-400">
                                <tr>
                                    <th className="px-6 py-3 w-1/2">Role</th>
                                    <th className="px-6 py-3 w-1/8">Level</th>
                                </tr>
                            </thead>
                            <tbody>
                            {roles.map(role =>
                                <tr key={role.role.id} className="border-b bg-gray-800 border-gray-700">
                                    <td className="px-6 py-4">
                                        <RoleDisplay role={role.role}/>
                                    </td>
                                    <td className="px-6 py-4 text-center">
                                        {role.level}
                                    </td>
                                </tr>)}
                            </tbody>

                        </table>
                        {roles.length === 0 ?
                            <div className="w-full flex items-center justify-center">
                                <span className="text-gray-400">No roles</span>
                            </div> : ''}
                </div>
            : ''}
        </>
    );
}