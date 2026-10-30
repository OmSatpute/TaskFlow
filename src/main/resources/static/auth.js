async function getCsrf() {
  const response=await fetch("/api/auth/csrf",{cache:"no-store",credentials:"same-origin"});
  if(!response.ok) throw new Error("Could not start a secure session. Refresh the page and try again.");
  return (await response.json()).token;
}
async function submitAuth(path,payload) {
  const token=await getCsrf();
  const response=await fetch(path,{method:"POST",headers:{"Content-Type":"application/json","X-CSRF-TOKEN":token},body:JSON.stringify(payload),credentials:"same-origin"});
  if(!response.ok){let message="Something went wrong. Please try again.";try{const error=await response.json();message=error.detail||error.message||message;}catch{}throw new Error(message);}
  return response.json();
}
const loginForm=document.getElementById("loginForm");
if(loginForm) loginForm.addEventListener("submit",async event=>{
  event.preventDefault();const error=document.getElementById("loginError");error.textContent="";
  try{await submitAuth("/api/auth/login",{email:document.getElementById("loginEmail").value.trim(),password:document.getElementById("loginPassword").value});location.replace("/workspace.html");}
  catch(e){error.textContent=e.message;}
});
const signupForm=document.getElementById("signupForm");
if(signupForm) signupForm.addEventListener("submit",async event=>{
  event.preventDefault();const error=document.getElementById("signupError");error.textContent="";
  try{await submitAuth("/api/auth/register",{name:document.getElementById("signupName").value.trim(),email:document.getElementById("signupEmail").value.trim(),password:document.getElementById("signupPassword").value,profile:document.getElementById("signupProfile").value});location.replace("/workspace.html");}
  catch(e){error.textContent=e.message;}
});
