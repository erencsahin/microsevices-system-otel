#!/usr/bin/env python3
"""
Simple load testing script for microservices system
Requires: pip install requests
"""

import requests
import random
import time
import concurrent.futures
from datetime import datetime

BASE_URL = "http://localhost:8080"

def create_user():
    """Create a random user"""
    user_data = {
        "name": f"User {random.randint(1, 10000)}",
        "email": f"user{random.randint(1, 10000)}@example.com",
        "phoneNumber": f"+9055512{random.randint(10000, 99999)}"
    }
    
    try:
        response = requests.post(f"{BASE_URL}/api/users", json=user_data)
        return {"status": response.status_code, "time": response.elapsed.total_seconds(), "service": "user"}
    except Exception as e:
        return {"status": "error", "error": str(e), "service": "user"}

def create_product():
    """Create a random product"""
    product_data = {
        "name": f"Product {random.randint(1, 10000)}",
        "description": "Load test product",
        "price": round(random.uniform(100, 50000), 2),
        "stockQuantity": random.randint(10, 1000),
        "category": random.choice(["Electronics", "Clothing", "Books", "Food"])
    }
    
    try:
        response = requests.post(f"{BASE_URL}/api/products", json=product_data)
        return {"status": response.status_code, "time": response.elapsed.total_seconds(), "service": "product"}
    except Exception as e:
        return {"status": "error", "error": str(e), "service": "product"}

def create_order():
    """Create a random order with service-to-service calls"""
    order_data = {
        "userId": random.randint(1, 100),
        "items": [
            {
                "productId": random.randint(1, 100),
                "quantity": random.randint(1, 5)
            },
            {
                "productId": random.randint(1, 100),
                "quantity": random.randint(1, 3)
            }
        ]
    }
    
    try:
        response = requests.post(f"{BASE_URL}/api/orders", json=order_data)
        return {"status": response.status_code, "time": response.elapsed.total_seconds(), "service": "order"}
    except Exception as e:
        return {"status": "error", "error": str(e), "service": "order"}

def get_users():
    """Get all users"""
    try:
        response = requests.get(f"{BASE_URL}/api/users")
        return {"status": response.status_code, "time": response.elapsed.total_seconds(), "service": "user-read"}
    except Exception as e:
        return {"status": "error", "error": str(e), "service": "user-read"}

def get_products():
    """Get all products"""
    try:
        response = requests.get(f"{BASE_URL}/api/products")
        return {"status": response.status_code, "time": response.elapsed.total_seconds(), "service": "product-read"}
    except Exception as e:
        return {"status": "error", "error": str(e), "service": "product-read"}

def run_mixed_load_test(duration_seconds=60, num_workers=50):
    """
    Run a mixed load test with multiple types of requests
    
    Args:
        duration_seconds: Duration of the test in seconds
        num_workers: Number of concurrent workers
    """
    print(f"Starting load test...")
    print(f"Duration: {duration_seconds} seconds")
    print(f"Workers: {num_workers}")
    print(f"Target: {BASE_URL}")
    print("-" * 50)
    
    start_time = time.time()
    end_time = start_time + duration_seconds
    
    results = []
    
    # Define test scenarios with weights
    scenarios = [
        (create_user, 0.3),      # 30% user creation
        (create_product, 0.3),   # 30% product creation
        (create_order, 0.2),     # 20% order creation (heavy operation)
        (get_users, 0.1),        # 10% read users
        (get_products, 0.1),     # 10% read products
    ]
    
    def run_scenario():
        """Run a random scenario based on weights"""
        while time.time() < end_time:
            # Select scenario based on weights
            rand = random.random()
            cumulative = 0
            selected_func = scenarios[0][0]
            
            for func, weight in scenarios:
                cumulative += weight
                if rand <= cumulative:
                    selected_func = func
                    break
            
            result = selected_func()
            results.append(result)
            
            # Small delay between requests
            time.sleep(random.uniform(0.1, 0.5))
    
    # Run test with concurrent workers
    with concurrent.futures.ThreadPoolExecutor(max_workers=num_workers) as executor:
        futures = [executor.submit(run_scenario) for _ in range(num_workers)]
        concurrent.futures.wait(futures)
    
    # Calculate statistics
    total_requests = len(results)
    successful = sum(1 for r in results if isinstance(r.get("status"), int) and r["status"] < 400)
    failed = total_requests - successful
    
    if successful > 0:
        avg_time = sum(r.get("time", 0) for r in results if "time" in r) / successful
        min_time = min((r.get("time", 0) for r in results if "time" in r), default=0)
        max_time = max((r.get("time", 0) for r in results if "time" in r), default=0)
    else:
        avg_time = min_time = max_time = 0
    
    # Print results
    print("\n" + "=" * 50)
    print("Load Test Results")
    print("=" * 50)
    print(f"Total Requests: {total_requests}")
    print(f"Successful: {successful}")
    print(f"Failed: {failed}")
    print(f"Success Rate: {(successful/total_requests*100):.2f}%")
    print(f"\nResponse Times:")
    print(f"  Average: {avg_time:.3f}s")
    print(f"  Min: {min_time:.3f}s")
    print(f"  Max: {max_time:.3f}s")
    print(f"\nRequests per second: {total_requests/duration_seconds:.2f}")
    
    # Service breakdown
    print(f"\nRequests by Service:")
    service_counts = {}
    for r in results:
        service = r.get("service", "unknown")
        service_counts[service] = service_counts.get(service, 0) + 1
    
    for service, count in sorted(service_counts.items()):
        print(f"  {service}: {count}")

if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description="Microservices Load Test")
    parser.add_argument("--duration", type=int, default=60, help="Test duration in seconds (default: 60)")
    parser.add_argument("--workers", type=int, default=50, help="Number of concurrent workers (default: 50)")
    parser.add_argument("--url", type=str, default="http://localhost:8080", help="Base URL (default: http://localhost:8080)")
    
    args = parser.parse_args()
    BASE_URL = args.url
    
    try:
        run_mixed_load_test(duration_seconds=args.duration, num_workers=args.workers)
    except KeyboardInterrupt:
        print("\n\nTest interrupted by user")
    except Exception as e:
        print(f"\nError during test: {e}")
