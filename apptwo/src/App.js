import './App.css';
import React, { useState, useEffect } from 'react'

function App() {

  const [user, setUser] = useState(null);

  useEffect(()=>{

    fetch("http://localhost:7071/api/me", {
      credentials: "include"
    }).then(res=>{
      if(res.status === 200){
        return res.json();
      }
      throw new Error('Not logged in');
    }).then(data=>{
      setUser(data)}).catch(()=>setUser(null));

  },[])

  const logout = () => {
    fetch("http://localhost:7071/api/logout", {
      method: "POST",
      credentials: "include"
    })
      .then(res => res.json())
      .then(data => {
        if (data.logoutUrl) {
          window.location.href = data.logoutUrl;
        } else {
          window.location.href = "/";
        }
      })
      .catch(err => {
        console.error(err);
      });
  };

  return (
    <div>
      {user ? (
        <>
        <h1>Hello, {user.name} in App B</h1>
        <button onClick={()=>logout()}>Logout</button>
        </>
      ):(
        <button onClick={()=> window.location.href = 'http://localhost:7071/api/login'}>Login</button>
      )}
    </div>
  );
}

export default App;