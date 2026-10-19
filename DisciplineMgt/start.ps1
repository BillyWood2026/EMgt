$jar = 'd:\EMgtv3\DisciplineMgt\target\DisciplineMgt-1.0.0.jar'
$out = 'd:\EMgtv3\DisciplineMgt\discipline8196.out.log'
$err = 'd:\EMgtv3\DisciplineMgt\discipline8196.err.log'
$proc = Start-Process -FilePath 'C:\Program Files\Java\jdk-17.0.18\bin\java.exe' `
  -ArgumentList '-Xmx128m','-Dfile.encoding=UTF-8','-jar',$jar `
  -RedirectStandardOutput $out -RedirectStandardError $err `
  -WindowStyle Hidden -PassThru
Write-Output "PID=$($proc.Id)"
