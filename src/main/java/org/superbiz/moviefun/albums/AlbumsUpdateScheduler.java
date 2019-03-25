package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater, DataSource dataSource) {
        this.albumsUpdater = albumsUpdater;
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }




    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    public void run() {
        try {

            if (shouldIUpdate()){

                jdbcTemplate.update("INSERT INTO album_scheduler_task (started_at) VALUES(?)", System.currentTimeMillis());

                logger.debug("Starting albums update");
                albumsUpdater.update();

                logger.debug("Finished albums update");
            }

        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }

    private boolean shouldIUpdate() {
        Optional<Long> OptionalMaxTimestamp = Optional.empty();
        OptionalMaxTimestamp = Optional.ofNullable(jdbcTemplate.queryForObject("SELECT MAX(started_at) FROM album_scheduler_task", Long.class));
        if (!(OptionalMaxTimestamp.isPresent())) return true;
        else {
            Long MaxTimestamp= OptionalMaxTimestamp.get();
            return MaxTimestamp < (System.currentTimeMillis()-120000);
        }
    }

}
