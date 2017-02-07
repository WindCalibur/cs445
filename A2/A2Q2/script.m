d=10;
e=5;

FASTAData = fastaread('data.fasta');
FSize = size(FASTAData);
FSize = FSize(1);
fData = FASTAData(1001);
firstString = getfield(fData, 'Sequence');
[num,txt,raw] = xlsread('BLOSOM.xlsx');
blosom = num;
blosomLegend = cell2mat(txt(1,2:end));
blosomLegend = [blosomLegend, 'U'];
% Add extra column for U
cIndex = strfind(blosomLegend,'C');
blosom2 = zeros(26,26);
blosom2(1:25,1:25) = blosom;
blosom2(26,1:25) = blosom(cIndex, :);
blosom2(1:25,26) = blosom(:, cIndex);
blosom2(26,26) = blosom(cIndex, cIndex);
blosom = blosom2;

resultIndex = [];
resultName = {};
resultScore = [];

for k = 1:1000
    fData = FASTAData(k);
    secondString = getfield(fData, 'Sequence');
    header = getfield(fData, 'Header');
    
    %Find name
    idx = strfind( header, '|');
    idx = idx(2) + 1;
    idxSpace = strfind( header, ' ');
    idxSpace = idxSpace(1)-1;
    geneName = header(idx:idxSpace);
    
    %Index
    index = k - 1;
    
    %Score Compuation
    [score, M, GX, GY] = A2Q2(firstString,secondString,d,e,blosom,blosomLegend);
    k
    
    %Finding 3 smallest scores
    if (k <= 3) 
        resultIndex(k) = index;
        resultName{k} = geneName;
        resultScore(k) = score;
    elseif (score > min(resultScore))
        [V,j] =  min(resultScore);
        resultIndex(j) = index;
        resultName{j} = geneName;
        resultScore(j) = score;
    end
    
    % Didn't bother sorting it out since we only have 3 results
    
end

    fData = FASTAData(resultIndex(1));
    secondString1 = getfield(fData, 'Sequence');
    
    fData = FASTAData(resultIndex(2));
    secondString2 = getfield(fData, 'Sequence');
    
    fData = FASTAData(resultIndex(3));
    secondString3 = getfield(fData, 'Sequence');

[string1, string2, start1, start2, length1] = backTrack(firstString,secondString1,d,e,blosom,blosomLegend);
[string3, string4, start3, start4, length2] = backTrack(firstString,secondString2,d,e,blosom,blosomLegend);
[string5, string6, start5, start6, length3] = backTrack(firstString,secondString3,d,e,blosom,blosomLegend);

string1 = string1(1:min(60, length1));
string2 = string2(1:min(60, length1));
string3 = string3(1:min(60, length2));
string4 = string4(1:min(60, length2));
string5 = string5(1:min(60, length3));
string6 = string6(1:min(60, length3));


