; HRM Platform installer hooks (electron-builder nsis include)
; Requires NSIS 3.x (Unicode). All messages in English for safety.

!macro customInit
    ; Check if TCP port 3306 is already in use
    nsExec::ExecToStack 'powershell -NoProfile -ExecutionPolicy Bypass -Command "$$c = Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue; if ($$c) { exit 0 } else { exit 1 }"'
    Pop $0
    ${If} $0 == 0
        MessageBox MB_ICONEXCLAMATION|MB_OKCANCEL "Port 3306 is already in use. HRM Platform needs port 3306 for its built-in database. You may continue the installation, but please free port 3306 before first start (for example, stop any existing MySQL service on this machine)." IDOK +2
        Abort
    ${EndIf}

    ; Check physical memory (recommend 16 GB, warn below 7.5 GB)
    nsExec::ExecToStack 'powershell -NoProfile -ExecutionPolicy Bypass -Command "[math]::Round((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB, 1)"'
    Pop $0
    Pop $1
    ${If} $0 == 0
        ${If} $1 < 7.5
            MessageBox MB_ICONINFORMATION|MB_OK "This machine has less than 8 GB of RAM. 16 GB is recommended for running HRM Platform smoothly. Installation will continue."
        ${EndIf}
    ${EndIf}
!macroend

!macro customInstall
    ; Write install-options.json consumed by the app on first launch
    FileOpen $0 "$INSTDIR\install-options.json" w
    FileWrite $0 '{"desktopShortcut":true}'
    FileClose $0
!macroend
