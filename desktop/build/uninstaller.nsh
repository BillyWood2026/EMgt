; HRM Platform uninstaller hooks (electron-builder nsis include)

!macro customUnInstall
    MessageBox MB_YESNO|MB_ICONQUESTION "Delete business data (database and uploaded files) in $APPDATA\HRM?" IDYES 0 IDNO +5
        IfFileExists "$APPDATA\HRM平台\*.*" 0 +2
            RMDir /r "$APPDATA\HRM平台"
        IfFileExists "$APPDATA\HRM\*.*" 0 +2
            RMDir /r "$APPDATA\HRM"
!macroend
