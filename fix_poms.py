import os
import sys

dirs = ['api-gateway', 'discovery-server', 'user-service', 'driver-service', 'trip-service', 'location-service', 'matching-service', 'notification-service']

for d in dirs:
    pom_path = os.path.join(d, 'pom.xml')
    if not os.path.exists(pom_path):
        continue
    with open(pom_path, 'r') as f:
        lines = f.readlines()
    
    new_lines = []
    skip = False
    for line in lines:
        stripped = line.strip()
        # Skip weird empty blocks I introduced
        if stripped == '<dependency>' or stripped == '</dependency>' or stripped == '<dependencies>' or stripped == '</dependencies>' or stripped == '<dependencyManagement>' or stripped == '</dependencyManagement>':
            # Only keep them if they are part of a valid structure (too complex for a quick script, I'll just target the ones with nothing between them)
            pass
        
    # Actually, the simplest way is to JUST REWRITE THE CRITICAL PARTS or use a proper XML parser.
    # But since I know the structure, I'll just use a reliable template for the dependencies section.
