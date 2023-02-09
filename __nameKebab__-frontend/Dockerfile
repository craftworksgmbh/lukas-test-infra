FROM nginx:1.23.3

ENV TZ=Europe/Vienna

# per-project config
ARG distpath=dist/__nameKebab__-frontend

# nginx config
COPY ./nginx/nginx.conf /etc/nginx/nginx.conf
COPY ./nginx/security-headers.conf /etc/nginx/security-headers.conf
WORKDIR /usr/share/nginx/html

# project artifacts
COPY $distpath .

RUN nginx -t

CMD ["nginx", "-g", "daemon off;"]
