import React from 'react';
import './App.css';
import {Leaderboard} from "./components/Leaderboard";

function App() {
  // @ts-ignore
  const serverId: bigint = window.serverId
  // @ts-ignore
  const userId: bigint = window.userId
  return (
      <>
          <div className="bg-slate-700 bg-cover min-h-screen">
              <Leaderboard serverId={serverId} userId={userId}/>
          </div>
      </>
  )
}

export default App;
