start: .env
	docker-compose up -d

stop: .env
	- docker-compose down

update: .env
	docker-compose pull
	- docker-compose down
	docker-compose up -d

backup:
	docker-compose exec --user postgres db pg_dump -U __nameLower__ __nameLower__ > "db-__nameLower__-$$(date +"%m-%d-%y_%H-%H-%S").sql"

#drop_db:
#	docker-compose exec --user postgres db dropdb -U __nameLower__ __nameLower__

import:
	docker-compose exec --user postgres db createdb -U __nameLower__ -O __nameLower__ __nameLower__
	cat dump.sql | docker-compose exec -T --user postgres db psql -U __nameLower__ __nameLower__


# To backup the DB call
# make backup

# To restore an old backup
# (there are manual steps involved, because removing the whole DB should not never ever happen 'by accident')
# 1) create the file dump.sql that contains the backup you want to restore (copy or move a file that was created by 'make backup' to dump.sql)
# 2) Stop the Backend (but not the DB)
# 3) uncomment the 'drop_db' section in the Makefile
# 4) call make drop_db
# 5) !!! make the drop_db section a comment again !!!
# 6) call make import
# 7) call make update