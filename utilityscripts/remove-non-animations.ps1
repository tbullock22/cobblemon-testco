Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/animations/ -Recurse | Where{$_.Name -Match ".bbmodel"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/animations/ -Recurse | Where{$_.Name -Match ".geo.json"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/animations/ -Recurse | Where{$_.Name -Match ".png"} | Remove-Item
