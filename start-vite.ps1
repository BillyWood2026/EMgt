Set-Location 'D:\EMgtv2\frontend'
$npm = 'C:\Program Files\nodejs\npm.cmd'
$env:PATH = 'C:\Program Files\nodejs;' + $env:PATH
& $npm run dev
