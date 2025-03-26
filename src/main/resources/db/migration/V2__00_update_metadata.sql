ALTER TABLE file_metadata MODIFY COLUMN application_id varchar(36);

ALTER TABLE image_metadata MODIFY COLUMN application_id varchar(36);

ALTER TABLE video_metadata MODIFY COLUMN application_id varchar(36);
