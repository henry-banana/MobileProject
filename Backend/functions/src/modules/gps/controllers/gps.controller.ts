import { Controller, Post, Get, Body, Query, UseGuards, Req, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { GpsService } from '../services/gps.service';
import { CreateOptimizedTripDto } from '../dto/create-optimized-trip.dto';
import { GetMyTripDto, ListMyTripsDto, StartTripDto, FinishTripDto } from '../dto/trip-query.dto';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators';
import { UserRole, AuthenticatedRequest } from '../../../core/interfaces/user.interface';

/**
 * GPS Controller
 *
 * Handles shipper route optimization and trip management endpoints.
 */
@ApiTags('GPS - Shipper Routing')
@Controller('gps')
@UseGuards(AuthGuard, RolesGuard)
@ApiBearerAuth('firebase-auth')
export class GpsController {
  constructor(private readonly gpsService: GpsService) {}

  /**
   * Create optimized trip for shipper
   *
   * Callable: POST /api/gps/create-optimized-trip
   * Auth: SHIPPER only
   */
  @Post('create-optimized-trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({
    summary: 'Create optimized delivery trip',
    description:
      'Creates a new trip with optimized route using Google Routes API. ' +
      'Takes 1-15 order IDs, extracts buildings, and computes optimal route.',
  })
  @ApiResponse({
    status: 201,
    description: 'Trip created successfully with optimized route',
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid request (e.g., too many orders, missing buildings, invalid order status)',
  })
  @ApiResponse({
    status: 401,
    description: 'Unauthorized - Not authenticated',
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Not a shipper',
  })
  @ApiResponse({
    status: 404,
    description: 'Order not found',
  })
  @ApiResponse({
    status: 500,
    description: 'Internal server error (e.g., Google Routes API failure)',
  })
  async createOptimizedTrip(@Req() req: AuthenticatedRequest, @Body() dto: CreateOptimizedTripDto) {
    const shipperId = req.user.uid;

    const trip = await this.gpsService.createOptimizedTrip(shipperId, dto);

    return {
      success: true,
      data: trip,
    };
  }

  /**
   * Get single trip by ID
   *
   * Callable: GET /api/gps/trip?tripId=...
   * Auth: SHIPPER only
   */
  @Get('trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Get trip by ID',
    description: 'Retrieves a single trip. Shipper must own the trip.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trip retrieved successfully',
  })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Trip belongs to another shipper',
  })
  @ApiResponse({
    status: 404,
    description: 'Trip not found',
  })
  async getMyTrip(@Req() req: AuthenticatedRequest, @Query() dto: GetMyTripDto) {
    const shipperId = req.user.uid;
    const trip = await this.gpsService.getMyTrip(shipperId, dto.tripId);

    return {
      success: true,
      data: trip,
    };
  }

  /**
   * List trips with pagination
   *
   * Callable: GET /api/gps/trips?status=PENDING&page=1&limit=20
   * Auth: SHIPPER only
   */
  @Get('trips')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'List my trips with pagination',
    description: 'Returns paginated list of trips for current shipper. Supports status filter.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trips retrieved successfully',
  })
  async listMyTrips(@Req() req: AuthenticatedRequest, @Query() dto: ListMyTripsDto) {
    const shipperId = req.user.uid;
    const result = await this.gpsService.listMyTrips(shipperId, dto.status, dto.page, dto.limit);

    return {
      success: true,
      data: result,
    };
  }

  /**
   * Start a trip (PENDING -> STARTED)
   *
   * Callable: POST /api/gps/start-trip
   * Auth: SHIPPER only
   */
  @Post('start-trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Start trip',
    description: 'Changes trip status from PENDING to STARTED. Sets startedAt timestamp.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trip started successfully',
  })
  @ApiResponse({
    status: 404,
    description: 'Trip not found',
  })
  @ApiResponse({
    status: 409,
    description: 'Conflict - Trip is not in PENDING status',
  })
  async startTrip(@Req() req: AuthenticatedRequest, @Body() dto: StartTripDto) {
    const shipperId = req.user.uid;
    const trip = await this.gpsService.startTrip(shipperId, dto.tripId);

    return {
      success: true,
      data: trip,
    };
  }

  /**
   * Finish a trip (STARTED -> FINISHED)
   *
   * Callable: POST /api/gps/finish-trip
   * Auth: SHIPPER only
   */
  @Post('finish-trip')
  @Roles(UserRole.SHIPPER)
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Finish trip',
    description: 'Changes trip status from STARTED to FINISHED. Sets finishedAt timestamp.',
  })
  @ApiResponse({
    status: 200,
    description: 'Trip finished successfully',
  })
  @ApiResponse({
    status: 404,
    description: 'Trip not found',
  })
  @ApiResponse({
    status: 409,
    description: 'Conflict - Trip is not in STARTED status',
  })
  async finishTrip(@Req() req: AuthenticatedRequest, @Body() dto: FinishTripDto) {
    const shipperId = req.user.uid;
    const result = await this.gpsService.finishTrip(shipperId, dto.tripId);

    return {
      success: true,
      data: result,
    };
  }
}
