-- This table is not limited to frame preferences anymore
ALTER TABLE frame_preference
RENAME TO banner_preference;

-- Rename old columns
ALTER TABLE banner_preference
RENAME COLUMN color TO frame_color;
ALTER TABLE banner_preference
RENAME COLUMN enabled TO frame_visible;

-- Add defaults
ALTER TABLE banner_preference
ALTER COLUMN frame_color SET DEFAULT NULL,
ALTER COLUMN frame_visible SET DEFAULT TRUE;

-- Add additional columns for more preference options
ALTER TABLE banner_preference
ADD COLUMN tag_visible BOOLEAN DEFAULT TRUE NOT NULL,
ADD COLUMN custom_status_visible BOOLEAN DEFAULT TRUE NOT NULL,
ADD COLUMN background_image_url TEXT DEFAULT NULL;