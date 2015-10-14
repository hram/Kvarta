"c:\Program Files\Git\bin\git.exe" checkout --orphan newBranch
"c:\Program Files\Git\bin\git.exe" add -A  
"c:\Program Files\Git\bin\git.exe" commit
"c:\Program Files\Git\bin\git.exe" branch -D master
"c:\Program Files\Git\bin\git.exe" branch -m master