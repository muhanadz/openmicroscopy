function thumbnailSet = getThumbnailByLongestSideSet(session, images, varargin)
% GETTHUMBNAILBYLONGESTSIDESET Retrieve a set of cache thumbnails from images on the OMERO server
%
%   thumbnailSet = getThumbnailByLongestSideSet(session, images) returns a
%   set of cache thumbnails for a series of input images where the aspect
%   ratio of the original images are preserved.
%
%   thumbnailSet = getThumbnailByLongestSideSet(session, images, size) also
%   sets the size of the longest side of the retrieved thumbnails.
%
%   thumbnailSet = getThumbnailByLongestSideSet(session, imageIDs) returns
%   a set of cache thumbnails for a series of input image identifiers where
%   the aspect ratio of the original images are preserved.
%
%   thumbnailSet = getThumbnailByLongestSideSet(session, imageIDs,  size)
%   also sets the size of the longest side of the retrieved thumbnails.
%
%   Examples:
%
%      thumbnailSet = getThumbnailByLongestSideSet(session, images);
%      thumbnailSet = getThumbnailByLongestSideSet(session, images, size);
%      thumbnailSet = getThumbnailByLongestSideSet(session, imageIDs);
%      thumbnailSet = getThumbnailByLongestSideSet(session, imageIDs, size);
%
% See also: GETTHUMBNAIL, GETTHUMBNAILBYLONGESTSIDE, GETTHUMBNAILSET

% Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
% All rights reserved.
%
% This program is free software; you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation; either version 2 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License along
% with this program; if not, write to the Free Software Foundation, Inc.,
% 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

% Define maximum number of thumbnails that can be retrieved at once
MAX_RETRIEVAL = 50;

% Input check
isValidImagesInput = @(x) isvector(x) &&...
    (isa(x(1), 'omero.model.ImageI') || isnumeric(x));
isValidThumbnailSize = @(x) isscalar(x) && x > 2 && round(x) == x;
ip = inputParser();
ip.addRequired('images', isValidImagesInput);
ip.addOptional('size', [], isValidThumbnailSize);
ip.parse(images, varargin{:});

% Format input thumbnail size
size = ip.Results.size;
if ~isempty(size), size = rint(size); end

% Get the pixels from the image
if isnumeric(images),
    images = getImages(session, ip.Results.images);
    assert(~isempty(images), 'No image found with ID: %u', ip.Results.images);
end

% Retrieve thumbnail set
imageIds = arrayfun(@(x) x.getId().getValue(), images);
pixelsIds = arrayfun(@(x) x.getPrimaryPixels().getId().getValue(), images);
nPixels = numel(pixelsIds);

% Create container service to load the thumbnails
context = java.util.HashMap;
group = images(1).getDetails().getGroup().getId().getValue();
context.put('omero.group', num2str(group));
store = session.createThumbnailStore();
pixelsRange = 1 : min(nPixels, MAX_RETRIEVAL);
thumbnailMap = store.getThumbnailByLongestSideSet(size,...
    toJavaList(pixelsIds(pixelsRange), 'java.lang.Long'), context);
store.close();

% Load remaining pixels by series of MAX_RETRIEVAL thumbnails
if nPixels > MAX_RETRIEVAL
    nIterations = 1 + floor((nPixels - 1)/MAX_RETRIEVAL - 1);
    for i = 1 : nIterations
        pixelsRange = i * MAX_RETRIEVAL + 1 : min(nPixels, (i + 1) * MAX_RETRIEVAL);
        store = session.createThumbnailStore();
        thumbnailMap.putAll(store.getThumbnailByLongestSideSet(size,...
            toJavaList(pixelsIds(pixelsRange), 'java.lang.Long'), context));
        store.close();
    end
end

% Fill cell array with thumbnailst
nThumbnails = thumbnailMap.size;
iterator = thumbnailMap.entrySet().iterator();
thumbnailSet(1 : nThumbnails) = struct('image', [], 'pixels', [], 'thumbnail', []);

for i = 1 : nThumbnails
    pairs = iterator.next();
    
    % Read map key and store pixels and image ID
    thumbnailSet(i).pixels = pairs.getKey();
    thumbnailSet(i).image = imageIds(pixelsIds == pairs.getKey());
    
    % Read map value and check byteArray
    byteArray = pairs.getValue();
    if isempty(byteArray)
        thumbnailSet(i).thumbnail = [];
    else
        % Convert byteArray into Matlab image
        stream = java.io.ByteArrayInputStream(pairs.getValue());
        image = javax.imageio.ImageIO.read(stream);
        stream.close();
        thumbnailSet(i).thumbnail = JavaImageToMatlab(image);
    end
end