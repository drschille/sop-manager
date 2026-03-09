sudo docker rm -f sop-web

sudo docker build -f Dockerfile.web \
  --no-cache \
  --build-arg VITE_CONVEX_URL="https://dynamic-fish-493.convex.cloud" \
  --build-arg VITE_MOCK_AUTH="true" \
  -t sop-web:latest .

sudo docker run -d --name sop-web -p 8080:80 sop-web:latest
