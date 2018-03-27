FROM node:latest
RUN apt-get update
RUN apt-get install apt-transport-https
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
RUN echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
RUN apt-get update && apt-get install yarn
RUN mkdir -p /moola-client
WORKDIR /moola-client
COPY moola-webapp/local/gulpfile.js /moola-client
COPY moola-webapp/local/package.json /moola-client
RUN yarn install

EXPOSE 3000
ENTRYPOINT ["node_modules/gulp/bin/gulp.js"]
CMD ["watch"]
