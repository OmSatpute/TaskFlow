fetch("/api/auth/me",{cache:"no-store",credentials:"same-origin"}).then(response=>{if(response.ok && response.status!==204) location.replace("/workspace.html");}).catch(()=>{});
