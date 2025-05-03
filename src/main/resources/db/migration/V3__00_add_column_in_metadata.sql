ALTER TABLE file_metadata
    ADD COLUMN access_level int;

ALTER TABLE image_metadata
    ADD COLUMN access_level int;

ALTER TABLE video_metadata
    ADD COLUMN access_level int;

update file_metadata set access_level = 0;

update image_metadata set access_level = 0;

update video_metadata set access_level = 0;