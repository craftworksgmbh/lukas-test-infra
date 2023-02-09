FROM cypress/base:18.12.1

# Copy files
COPY . /e2e
WORKDIR /e2e

# Install dependencies
RUN npm ci
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

# Start application
ENTRYPOINT []
CMD /wait && npm run e2e:run