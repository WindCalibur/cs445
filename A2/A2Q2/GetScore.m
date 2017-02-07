

function [ score ] = GetScore( first, second, blosom, blosomLegend)
    firstIndex = strfind(blosomLegend,first);
    secondIndex = strfind(blosomLegend,second);
    score = blosom(firstIndex, secondIndex);
end

