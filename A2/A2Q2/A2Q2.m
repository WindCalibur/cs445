function [ score, M, GX, GY ] = A2Q2( firstString, secondString, d, e, blosom, blosomLegend )
    
    length1 = length(firstString);
    length2 = length(secondString);
    
    % All index off by 1 HIGHER because matlab does not allow 0 as index
    M = zeros(length1+1, length2+1);
    GX = zeros(length1+1, length2+1);
    GY = zeros(length1+1, length2+1);
    
    % Initialization
    
    M(1,1) = 0;
    GX(1,1) = -Inf;
    GY(1,1) = -Inf;
    for i = 1:length1
        M(i+1,1) = 0;
        GX(i+1,1) = -d;
        GY(i+1,1) = -Inf;
    end
    for j = 1:length2
        M(1,j+1) = 0;
        GX(1,j+1) = -Inf;
        GY(1,j+1) = -d;
    end
    
    for i = 1:length1
        for j = 1:length2
            % M
            M(i+1,j+1) = max(GetScore(firstString(i), secondString(j),blosom,blosomLegend) + max([M(i,j) GX(i,j), GY(i,j)]), 0);
            % GX
            GX(i+1,j+1) = max((GX(i,j+1)-e), (M(i,j+1)-d));
            % GY
            GY(i+1,j+1) = max((GY(i+1,j)-e), (M(i+1,j)-d));
        end
    end
    
    [score,I] = max(M(:));
end

