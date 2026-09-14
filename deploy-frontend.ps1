# 一键把本地前端产物同步到虚拟机上的 Nginx
# 用法（在项目根目录执行）：
#   powershell -ExecutionPolicy Bypass -File .\deploy-frontend.ps1
# 可选指定虚拟机地址：
#   powershell -ExecutionPolicy Bypass -File .\deploy-frontend.ps1 root@192.168.209.128

param(
  [string]$VmHost = "root@192.168.209.128",
  [string]$NginxContainer = "campus-nginx",
  [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$tarName = "dist-upload.tar.gz"
$tarPath = Join-Path $root $tarName

# [1/4] 构建前端
if (-not $SkipBuild) {
  Write-Host "[1/4] 构建前端 (npm run build)..." -ForegroundColor Cyan
  Set-Location (Join-Path $root "frontend")
  npm run build
  if ($LASTEXITCODE -ne 0) { throw "前端构建失败，已中止。" }
  Set-Location $root
} else {
  Write-Host "[1/4] 跳过构建 (-SkipBuild)" -ForegroundColor DarkGray
}

if (-not (Test-Path (Join-Path $root "frontend\dist\index.html"))) {
  throw "未找到 frontend\dist\index.html，请先构建前端。"
}

# [2/4] 打包 dist
Write-Host "[2/4] 打包 frontend\dist ..." -ForegroundColor Cyan
if (Test-Path $tarPath) { Remove-Item $tarPath -Force }
Push-Location (Join-Path $root "frontend")
tar -czf $tarPath dist
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "打包失败。" }

# [3/4] 上传到虚拟机（此处会提示输入 SSH 密码）
Write-Host "[3/4] 上传到 $VmHost (需要输入 SSH 密码)..." -ForegroundColor Cyan
scp $tarPath "${VmHost}:/tmp/$tarName"
if ($LASTEXITCODE -ne 0) { Remove-Item $tarPath -Force -ErrorAction SilentlyContinue; throw "上传失败。" }

# [4/4] 远端解包替换并重启 nginx（再次提示输入密码）
Write-Host "[4/4] 替换 Nginx 目录并重启容器..." -ForegroundColor Cyan
$remoteScript = @'
set -e
CONTAINER="__CONTAINER__"
if ! docker inspect "$CONTAINER" >/dev/null 2>&1; then
  echo "找不到容器 $CONTAINER，请用 -NginxContainer 指定正确名称"
  docker ps --format '{{.Names}}'
  exit 1
fi
TARGET="$(docker inspect "$CONTAINER" --format '{{range .Mounts}}{{if eq .Destination "/usr/share/nginx/html"}}{{.Source}}{{end}}{{end}}')"
if [ -z "$TARGET" ]; then
  echo "未找到 $CONTAINER 的 /usr/share/nginx/html 挂载"
  docker inspect "$CONTAINER" --format '{{range .Mounts}}{{.Source}} -> {{.Destination}}{{"\n"}}{{end}}'
  exit 1
fi
echo "Nginx 静态目录: $TARGET"
mkdir -p "$TARGET"
rm -rf "${TARGET:?}"/*
tar -xzf "/tmp/__TAR__" -C "$TARGET" --strip-components=1
rm -f "/tmp/__TAR__"
docker restart "$CONTAINER" >/dev/null
echo "FRONTEND_DEPLOYED_OK"
'@
$remoteScript = $remoteScript.Replace("__CONTAINER__", $NginxContainer).Replace("__TAR__", $tarName)

$remoteScript | ssh $VmHost "bash -s"

Remove-Item $tarPath -Force -ErrorAction SilentlyContinue
Write-Host "完成。浏览器请按 Ctrl + F5 强制刷新。" -ForegroundColor Green
