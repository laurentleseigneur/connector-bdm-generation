FROM postgres:11.9

USER postgres

COPY --chown=postgres:postgres ./dvdrental/*.sql /docker-entrypoint-initdb.d/
COPY --chown=postgres:postgres ./dvdrental/*.dat /docker-entrypoint-initdb.d/

ENTRYPOINT ["docker-entrypoint.sh"]

CMD ["postgres"]